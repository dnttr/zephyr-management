#version 330 core

layout (location = 0) in vec2 r_shape_position;
layout (location = 1) in vec2 r_tex_coords;

uniform mat4 projection_matrix;

out vec2 coords;

void main()
{
    coords = r_tex_coords;
    gl_Position = vec4(r_shape_position * 2.0 - 1.0, 0.0, 1.0);
}