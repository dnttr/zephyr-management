#version 330 core

layout (location = 0) in vec4 vertex;

out vec2 TexCoords;

uniform mat4 projection;
uniform vec2 textPosition;
uniform vec2 scale;

void main()
{
    vec2 pos = vertex.xy * scale + textPosition;
    gl_Position = projection * vec4(pos, 0.0, 1.0);
    TexCoords = vertex.zw;
}
