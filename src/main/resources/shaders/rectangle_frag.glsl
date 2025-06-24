#version 330 core

out vec4 FragColor;
in vec2 coords;

uniform vec3 shape_color;

uniform vec2 shape_position;
uniform vec2 rectangle_size;

uniform float rectangle_radius;
uniform float shape_opacity;

void main() {
    vec2 pos = coords - shape_position;

    vec2 dist = abs(pos - rectangle_size * 0.5) - (rectangle_size * 0.5) + vec2(rectangle_radius);
    float d = length(max(dist, 0.0)) - rectangle_radius;

    float edgeWidth = clamp(fwidth(d) * 1.5, 0.5, 3.0);

    float alpha = 1.0 - smoothstep(0.0, edgeWidth, d);

    if (alpha < 0.001) {
        discard;
    }

    FragColor = vec4(shape_color, shape_opacity * alpha);
}