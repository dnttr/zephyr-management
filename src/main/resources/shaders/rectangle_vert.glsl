#version 330 core

layout (location = 0) in vec3 r_shape_position;

uniform mat4 projection_matrix;
uniform vec2 shape_position;
uniform vec2 shape_size;
out vec2 coords;

void main()
{
    vec2 pixel_position = r_shape_position.xy * shape_size + shape_position;

    gl_Position = projection_matrix * vec4(pixel_position, r_shape_position.z, 1.0);
    coords = r_shape_position.xy;
}