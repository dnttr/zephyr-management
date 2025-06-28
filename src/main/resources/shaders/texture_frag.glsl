#version 330 core

out vec4 FragColor;

in vec2 coords;

uniform sampler2D tex;
uniform float tex_alpha;

void main() {
    vec4 finalColor = texture(tex, coords);

    if (finalColor.a < 0.01) {
        discard;
    }

    finalColor.a = tex_alpha * smoothstep(0.01, 1.0, finalColor.a);
    FragColor = finalColor;
}