#version 330 core

out vec4 FragColor;

in vec2 coords;

uniform sampler2D tex;
uniform sampler2D mask_tex;
uniform float radius;
uniform vec2 size;
uniform vec3 tint_color;
uniform float tint_strength;
uniform float effect_alpha;
uniform vec3 outline_effect_color;
uniform float outline_effect_strength;

vec4 sampleTextureSafe(sampler2D sampler, vec2 uv) {
    return texture(sampler, clamp(uv, vec2(0.0), vec2(1.0)));
}

float createConnectedGradient(vec2 uv, float angle, float line_spacing) {
    float cos_a = cos(angle);
    float sin_a = sin(angle);
    vec2 rotated_uv = vec2(uv.x * cos_a - uv.y * sin_a, uv.x * sin_a + uv.y * cos_a);

    float line_position = rotated_uv.x / line_spacing;
    float line_intensity = 1.0 - fract(line_position);

    line_intensity = pow(line_intensity, 3.0);

    float vertical_fade = 1.0 - uv.y;
    vertical_fade = pow(vertical_fade, 1.5);

    return line_intensity * vertical_fade;
}

void main() {
    vec4 original = sampleTextureSafe(tex, coords);
    float mask_alpha = sampleTextureSafe(mask_tex, coords).a;

    if (mask_alpha < 0.0001) {
        FragColor = original;
        return;
    }

    float smooth_alpha = smoothstep(0.05, 0.2, mask_alpha);

    vec2 texel_size = 1.0 / size;
    vec3 color = vec3(0.0);
    float total_weight = 0.0;

    vec2 center_offset = coords - vec2(0.5);
    float distance_from_center = length(center_offset);
    float blur_multiplier = 0.7 + distance_from_center * 0.6;
    float effective_radius = radius * blur_multiplier;

    int base_samples = int(effective_radius * 0.15);
    base_samples = clamp(base_samples, 8, 32);

    int inner_samples = base_samples;
    for (int i = 0; i < inner_samples; i++) {
        float angle = float(i) * 6.283185307 / float(inner_samples);
        float layer_radius = effective_radius * 0.3;
        vec2 direction = vec2(cos(angle), sin(angle));
        vec2 offset = direction * layer_radius * texel_size;

        vec2 aberration = direction * 0.0008;
        float red   = sampleTextureSafe(tex, coords + offset - aberration).r;
        float green = sampleTextureSafe(tex, coords + offset).g;
        float blue  = sampleTextureSafe(tex, coords + offset + aberration).b;

        float weight = 1.2;
        color += vec3(red, green, blue) * weight;
        total_weight += weight;
    }

    int mid_samples = base_samples + 8;
    for (int i = 0; i < mid_samples; i++) {
        float angle = float(i) * 6.283185307 / float(mid_samples);
        float layer_radius = effective_radius * 0.6;
        vec2 direction = vec2(cos(angle), sin(angle));
        vec2 offset = direction * layer_radius * texel_size;

        vec2 aberration = direction * 0.0012;
        float red   = sampleTextureSafe(tex, coords + offset - aberration).r;
        float green = sampleTextureSafe(tex, coords + offset).g;
        float blue  = sampleTextureSafe(tex, coords + offset + aberration).b;

        float weight = 0.8;
        color += vec3(red, green, blue) * weight;
        total_weight += weight;
    }

    int outer_samples = base_samples + 16;
    for (int i = 0; i < outer_samples; i++) {
        float angle = float(i) * 6.283185307 / float(outer_samples);
        float layer_radius = effective_radius;
        vec2 direction = vec2(cos(angle), sin(angle));
        vec2 offset = direction * layer_radius * texel_size;

        vec2 aberration = direction * 0.0015;
        float red   = sampleTextureSafe(tex, coords + offset - aberration).r;
        float green = sampleTextureSafe(tex, coords + offset).g;
        float blue  = sampleTextureSafe(tex, coords + offset + aberration).b;

        float normalized_distance = layer_radius / effective_radius;
        float weight = exp(-normalized_distance * normalized_distance * 2.0) * 0.4;
        color += vec3(red, green, blue) * weight;
        total_weight += weight;
    }

    vec3 blurred_rgb = color / total_weight;

    vec3 tinted_rgb = mix(blurred_rgb, tint_color, tint_strength);

    vec2 pixel_size = 1.0 / size;
    float edge_detection = 0.0;

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            if (x == 0 && y == 0) continue;
            vec2 offset = vec2(float(x), float(y)) * pixel_size * 2.0;
            float neighbor_alpha = sampleTextureSafe(mask_tex, coords + offset).a;
            edge_detection += abs(mask_alpha - neighbor_alpha) / 10;
        }
    }

    float boosted_edge_detection = edge_detection * 15.0;
    float rim_intensity = smoothstep(0.01, 1.0, boosted_edge_detection);
    rim_intensity *= smoothstep(0.01, 1.0, mask_alpha);
    rim_intensity = sqrt(rim_intensity);

    vec3 rim_light_color = vec3(0.8, 0.9, 1.0);
    float rim_boost_factor = 5.0;
    vec3 rim_light = rim_light_color * rim_intensity * rim_boost_factor;

    vec2 highlight_pos = vec2(0.3, 0.3);
    float highlight_dist = distance(coords, highlight_pos);
    float highlight_falloff = pow(max(0.0, 1.0 - highlight_dist * 3.0), 16.0);
    float specular_boost = 0.8;
    vec3 specular_highlight = vec3(1.0) * highlight_falloff * 0.5 * mask_alpha * specular_boost;

    float gradient_angle = radians(30.0);
    float line_spacing = 0.08;
    float gradient_intensity = createConnectedGradient(coords, gradient_angle, line_spacing);
    float gradient_boost = 1.5;
    vec3 gradient_color = vec3(1.0, 1.0, 1.0) * gradient_intensity * 0.25 * mask_alpha * gradient_boost;

    vec2 refraction_offset = normalize(center_offset) * 0.005 * mask_alpha;
    vec3 refracted_bg = sampleTextureSafe(tex, coords + refraction_offset).rgb;

    vec3 base_glass_effect = mix(tinted_rgb, refracted_bg, 0.3 * mask_alpha);

    vec3 final_output_rgb = base_glass_effect;

    final_output_rgb += rim_light;
    final_output_rgb += specular_highlight;
    final_output_rgb += gradient_color;

    float fresnel = pow(1.0 - dot(normalize(center_offset), vec2(0.0, 1.0)), 2.0);
    final_output_rgb = mix(final_output_rgb, final_output_rgb * 1.2, fresnel * 0.3);

    FragColor = vec4(final_output_rgb, smooth_alpha * effect_alpha);
}