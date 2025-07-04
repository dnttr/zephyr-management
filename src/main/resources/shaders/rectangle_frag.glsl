#version 330 core

out vec4 FragColor;
in vec2 coords;

uniform vec3 shape_color;
uniform float shape_opacity;

uniform float shape_radius;
uniform vec2 shape_size;

uniform vec3 outline_color;
uniform float outline_width;
uniform float outline_opacity;

// G2 continuity rounded rectangle SDF by Inigo Quilez
float roundedBoxSDF(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b;
    float d = length(max(q, 0.0));
    return d + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    vec2 pixel_coords_local = coords * shape_size;
    vec2 center = shape_size * 0.5;
    vec2 half_size = shape_size * 0.5;

    float r = min(shape_radius, min(half_size.x, half_size.y));

    float d = roundedBoxSDF(pixel_coords_local - center, half_size - r, r);

    float edge_width = fwidth(d);

    float outline_region = d + outline_width;

    float shape_alpha = 1.0 - smoothstep(-edge_width, edge_width, d);
    float outline_mask = 1.0 - smoothstep(-edge_width, edge_width, outline_region);

    float outline_only = outline_mask - shape_alpha;

    vec3 final_color = shape_color;
    float final_alpha = shape_alpha * shape_opacity;

    if (outline_width > 0.0 && outline_opacity > 0.0) {
        float color_mix_factor = outline_only * outline_opacity;

        final_color = mix(final_color, outline_color, color_mix_factor);
        final_alpha = max(final_alpha, outline_only * outline_opacity);
    }

    if (final_alpha < 0.005) {
        discard;
    }

    FragColor = vec4(final_color, final_alpha);
}