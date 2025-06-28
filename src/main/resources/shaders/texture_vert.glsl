#version 330 core

layout (location = 0) in vec3 r_tex_position;
layout (location = 1) in vec2 r_tex_coords;

uniform mat4 projection_matrix;

uniform vec2 tex_position;
uniform vec2 tex_size;

out vec2 coords;

void main()
{
    vec3 scaledPos = vec3(r_tex_position.xy * tex_size + tex_position, r_tex_position.z);
    gl_Position = projection_matrix * vec4(scaledPos, 1.0);

    coords = r_tex_coords;
}