precision mediump float;

uniform sampler2D u_Texture;//纹理内容数据
uniform vec4 u_Color;

//接收从顶点着色器过来的参数
varying vec2 v_TexCoord;

void main() {
   //将计算出的颜色给此片元
   vec4 finalColor = texture2D(u_Texture, vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));
   //给此片元颜色值
   gl_FragColor = finalColor;

   //gl_FragColor = u_Color;
}