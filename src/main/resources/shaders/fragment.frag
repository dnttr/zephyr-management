#version 330 core

out vec4 FragColor;

uniform vec2 rectPos;
uniform vec2 rectSize;
uniform float radius;
uniform vec2 resolution;

void main() {
    vec2 fragPos = vec2(gl_FragCoord.x, resolution.y - gl_FragCoord.y);
    vec2 pos = fragPos - rectPos;

    vec2 dist = abs(pos - rectSize * 0.5) - (rectSize * 0.5) + vec2(radius);
    float d = length(max(dist, 0.0)) - radius;

    float edgeWidth = fwidth(d) * 1.5;

    float alpha = 1.0 - smoothstep(0.0, edgeWidth, d);

    if (alpha < 0.001) {
        discard;
    }

    FragColor = vec4(1.0, 0.2, 0.2, alpha);
}