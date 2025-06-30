#version 330 core

in vec2 coords;
out vec4 FragColor;

uniform sampler2D tex;
uniform float blur_radius;
uniform vec2 tex_size;
uniform vec3 tint_color;
uniform float tint_strength;
uniform int sample_count;

void main() {
    vec2 tex_offset = 1.0 / tex_size;
    vec3 color = vec3(0.0);
    float total_weight = 0.0;

    for (int i = 0; i < sample_count; i++) {
        float angle = float(i) * 6.283185307 / float(sample_count);

        for (int ring = 1; ring <= 3; ring++) {
            float distance = blur_radius * float(ring) / 3.0;
            vec2 offset = vec2(cos(angle), sin(angle)) * distance * tex_offset;

            float weight = exp(-0.5 * pow(distance / blur_radius, 2.0));
            color += texture(tex, coords + offset).rgb * weight;
            total_weight += weight;
        }
    }

    float center_weight = 1.0;
    color += texture(tex, coords).rgb * center_weight;
    total_weight += center_weight;

    color /= total_weight;

    vec3 tinted_color = mix(color, color * tint_color, tint_strength);
    FragColor = vec4(tinted_color, texture(tex, coords).a);
}