#version 330 core

out vec4 FragColor;

uniform vec2 rectangle_position;
uniform vec2 rectangle_size;
uniform vec2 screen_resolution;

uniform vec3 rectangle_color;

uniform float rectangle_corner_radius;
uniform float rectangle_opacity;

void main() {
    vec2 fragPos = vec2(gl_FragCoord.x, screen_resolution.y - gl_FragCoord.y);
    vec2 pos = fragPos - rectangle_position;

    vec2 dist = abs(pos - rectangle_size * 0.5) - (rectangle_size * 0.5) + vec2(rectangle_corner_radius);
    float d = length(max(dist, 0.0)) - rectangle_corner_radius;

    float edgeWidth = fwidth(d) * 1.5;

    float alpha = rectangle_opacity * (1.0 - smoothstep(0.0, edgeWidth, d));

    if (alpha < 0.001) {
        discard;
    }

    FragColor = vec4(rectangle_color, alpha);
}