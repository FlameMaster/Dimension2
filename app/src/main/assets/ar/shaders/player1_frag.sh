precision mediump float;
uniform sampler2D sTexture;//纹理内容数据
//接收从顶点着色器过来的参数
varying vec4 ambient;
varying vec4 diffuse;
varying vec4 specular;
varying vec2 vTextureCoord;

// alpha值
uniform float uOpacity;

void main()
{
   //将计算出的颜色给此片元
      vec4 finalColor = texture2D(sTexture, vec2(vTextureCoord.x, 1.0 - vTextureCoord.y));
      finalColor.a *= uOpacity;
      //给此片元颜色值
      gl_FragColor = finalColor*ambient+finalColor*specular+finalColor*diffuse;
}   