/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

precision mediump float;

uniform sampler2D u_Texture;

uniform vec4 u_LightingParameters;
uniform vec4 u_MaterialParameters;
uniform vec4 u_ColorCorrectionParameters;

varying vec3 v_ViewPosition;
varying vec3 v_ViewNormal;
varying vec2 v_TexCoord;
uniform vec4 u_ObjColor;

void main() {
    // 我们支持近似sRGB伽玛.
    const float kGamma = 0.4545454;
    const float kInverseGamma = 2.2;
    const float kMiddleGrayGamma = 0.466;

    // 打开照明和材料参数，以便更好地命名.
    vec3 viewLightDirection = u_LightingParameters.xyz;
    vec3 colorShift = u_ColorCorrectionParameters.rgb;
    float averagePixelIntensity = u_ColorCorrectionParameters.a;

    float materialAmbient = u_MaterialParameters.x;
    float materialDiffuse = u_MaterialParameters.y;
    float materialSpecular = u_MaterialParameters.z;
    float materialSpecularPower = u_MaterialParameters.w;

    // 规范化变化的参数，因为它们在顶点着色器中是线性插值的.
    vec3 viewFragmentDirection = normalize(v_ViewPosition);
    vec3 viewNormal = normalize(v_ViewNormal);

    // 翻转y纹理坐标来从左上角处理纹理.
    vec4 objectColor = texture2D(u_Texture, vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));

    // 只有当u_ObjColor的alpha值为l到255.0时，才对灰度图像应用颜色.
    objectColor.rgb *= mix(vec3(1.0), u_ObjColor.rgb / 255.0,
                           step(255.0, u_ObjColor.a));

    // 在进行照明计算之前，对纹理应用逆SRGB伽马.
    objectColor.rgb = pow(objectColor.rgb, vec3(kInverseGamma));

    // 环境光不受光强的影响.
    float ambient = materialAmbient;

    // 近似于半球光(不是刺眼的方向光).
    float diffuse = materialDiffuse *
            0.5 * (dot(viewNormal, viewLightDirection) + 1.0);

    // 计算反射光.
    vec3 reflectedLightDirection = reflect(viewLightDirection, viewNormal);
    float specularStrength = max(0.0, dot(viewFragmentDirection, reflectedLightDirection));
    float specular = materialSpecular *
            pow(specularStrength, materialSpecularPower);

    vec3 color = objectColor.rgb * (ambient + diffuse) + specular;
    // 在写入片段颜色之前应用SRGB伽马.
    color.rgb = pow(color, vec3(kGamma));
    // 应用平均像素强度和颜色变化
    color *= colorShift * (averagePixelIntensity / kMiddleGrayGamma);
    gl_FragColor.rgb += color;
    //gl_FragColor.a = objectColor.a;
}
