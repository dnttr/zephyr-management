#version 330 core

layout (location = 0) in vec3 r_shape_position;

uniform mat4 projection_matrix;

out vec2 coords;

void main()
{
    gl_Position = projection_matrix * vec4(r_shape_position, 1.0);
    coords = r_shape_position.xy;
}