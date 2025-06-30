#version 330 core

out vec4 FragColor;

in vec2 local_position;

uniform vec3 ball_color;

void main() {
    float distance_to_center = length(local_position);

    float blend_with = fwidth(distance_to_center) / 2;
    float alpha = smoothstep(1.0 + blend_with, 1.0 - blend_with, distance_to_center);

    if (alpha < 0.01) {
        discard;
    }

    FragColor = vec4(ball_color, alpha);
}