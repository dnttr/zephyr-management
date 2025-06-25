#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 m_projection_matrix;

uniform vec2 m_texture_position;
uniform vec2 m_texture_size;

out vec2 coords;

void main()
{
    vec3 scaledPos = vec3(aPos.xy * m_texture_size + m_texture_position, aPos.z);
    gl_Position = m_projection_matrix * vec4(scaledPos, 1.0);
    coords = aTexCoord;
}