precision mediump float;

uniform sampler2D sTexture;//纹理内容数据
uniform vec4 vColor;

//接收从顶点着色器过来的参数
varying vec2 v_TexCoord;

void main() {
   //gl_FragColor = vColor;
   //将计算出的颜色给此片元
   vec4 finalColor=texture2D(sTexture, v_TexCoord);
   //给此片元颜色值
   gl_FragColor = finalColor;
}