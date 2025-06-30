#version 330 core

out vec4 FragColor;

in vec2 local_position;

void main() {
    float distance_to_center = length(local_position);

    float blend_with = 0.01;
    float alpha = smoothstep(1.0 + blend_with, 1.0 - blend_with, distance_to_center);

    if (alpha < 0.01) {
        discard;
    }

    FragColor = vec4(1.0, 0.5, 1.0, alpha);
}