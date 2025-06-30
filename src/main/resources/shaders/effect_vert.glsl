#version 330 core

layout (location = 0) in vec2 r_shape_position;
layout (location = 1) in vec2 r_shape_instance_position;

out vec2 local_position;

uniform float ball_radius;
uniform mat4 projection;

void main() {
    local_position = (r_shape_position - 0.5) * 2.0;

    vec2 transformed_position = r_shape_position * ball_radius + r_shape_instance_position;
    gl_Position = projection * vec4(transformed_position, 0.0, 1.0);
}