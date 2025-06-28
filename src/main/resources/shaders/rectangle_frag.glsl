#version 330 core

out vec4 FragColor;
in vec2 coords;

uniform vec3 shape_color;
uniform float shape_opacity;

uniform vec2 shape_position;
uniform float shape_radius;
uniform vec2 shape_size;

void main() {
    vec2 position = coords - shape_position;
    vec2 distance = abs(position - shape_size * 0.5) - (shape_size * 0.5) + vec2(shape_radius);

    float d = length(max(distance, 0.0)) - shape_radius;
    float edge_width = clamp(fwidth(d) * 1.5, 0.5, 3.0);
    float alpha = 1.0 - smoothstep(0.0, edge_width, d);

    if (alpha < 0.001) {
        discard;
    }

    FragColor = vec4(shape_color, shape_opacity * alpha);
}