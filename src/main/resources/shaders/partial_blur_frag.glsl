#version 330 core
out vec4 FragColor;
in vec2 coords;

uniform sampler2D tex;
uniform sampler2D mask_tex;
uniform float radius;
uniform vec2 size;
uniform vec3 tint_color;
uniform float tint_strength;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fractal_noise(vec2 p, int octaves) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < octaves; i++) {
        value += amplitude * noise(p * frequency);
        amplitude *= 0.5;
        frequency *= 2.0;
    }
    return value;
}

void main() {
    vec4 original = texture(tex, coords);
    float mask_alpha = texture(mask_tex, coords).a;

    if (mask_alpha < 0.01) {
        FragColor = original;
        return;
    }

    vec2 texel_size = 1.0 / size;
    vec3 color = vec3(0.0);
    float total_weight = 0.0;

    vec2 center_offset = coords - vec2(0.5);
    float distance_from_center = length(center_offset);

    vec2 frost_coords = coords * size * 0.003;
    float base_frost_pattern = fractal_noise(frost_coords, 4);
    base_frost_pattern = smoothstep(0.5, 0.9, base_frost_pattern);

    float edge_factor = smoothstep(0.15, 0.5, distance_from_center);
    float frost_pattern = base_frost_pattern * edge_factor * 0.3;

    float blur_multiplier = 0.7 + distance_from_center * 0.6;
    float effective_radius = radius * blur_multiplier;

    effective_radius *= (1.0 + frost_pattern * 0.1);

    int base_samples = int(effective_radius * 0.25);
    base_samples = clamp(base_samples, 12, 48);

    vec2 frost_distortion = vec2(
    fractal_noise(coords * size * 0.005 + vec2(0.0, 1.0), 3),
    fractal_noise(coords * size * 0.005 + vec2(1.0, 0.0), 3)
    ) - 0.5;
    frost_distortion *= frost_pattern * 0.8;

    vec2 noise_base = (vec2(hash(coords), hash(coords + 0.1)) - 0.5) * 0.3 + frost_distortion;

    int inner_samples = base_samples;
    for (int i = 0; i < inner_samples; i++) {
        float angle = float(i) * 6.283185307 / float(inner_samples);
        float layer_radius = effective_radius * 0.3;

        vec2 direction = vec2(cos(angle), sin(angle));
        vec2 noise = noise_base * texel_size * 0.5;
        vec2 offset = direction * layer_radius * texel_size + noise;

        vec2 aberration = direction * 0.0008;
        float red = texture(tex, coords + offset - aberration).r;
        float green = texture(tex, coords + offset).g;
        float blue = texture(tex, coords + offset + aberration).b;

        float weight = 1.2;
        color += vec3(red, green, blue) * weight;
        total_weight += weight;
    }

    int mid_samples = base_samples + 8;
    for (int i = 0; i < mid_samples; i++) {
        float angle = float(i) * 6.283185307 / float(mid_samples);
        float layer_radius = effective_radius * 0.6;

        vec2 direction = vec2(cos(angle), sin(angle));
        vec2 noise = noise_base * texel_size * 0.7;
        vec2 offset = direction * layer_radius * texel_size + noise;

        vec2 aberration = direction * 0.0012;
        float red = texture(tex, coords + offset - aberration).r;
        float green = texture(tex, coords + offset).g;
        float blue = texture(tex, coords + offset + aberration).b;

        float weight = 0.8;
        color += vec3(red, green, blue) * weight;
        total_weight += weight;
    }

    int outer_samples = base_samples + 16;
    for (int i = 0; i < outer_samples; i++) {
        float angle = float(i) * 6.283185307 / float(outer_samples);
        float layer_radius = effective_radius;

        vec2 direction = vec2(cos(angle), sin(angle));
        vec2 noise = noise_base * texel_size;
        vec2 offset = direction * layer_radius * texel_size + noise;

        vec2 aberration = direction * 0.0015;
        float red = texture(tex, coords + offset - aberration).r;
        float green = texture(tex, coords + offset).g;
        float blue = texture(tex, coords + offset + aberration).b;

        float normalized_distance = layer_radius / effective_radius;
        float weight = exp(-normalized_distance * normalized_distance * 2.0) * 0.4;

        color += vec3(red, green, blue) * weight;
        total_weight += weight;
    }

    color += original.rgb * 2.5;
    total_weight += 2.5;

    color /= total_weight;

    vec3 tinted = mix(color, tint_color, tint_strength);

    float overall_whitening = 0.25;
    tinted = mix(tinted, vec3(1.0), overall_whitening);

    float frost_brightness = 1.0 + frost_pattern * 0.15;
    tinted *= frost_brightness;

    vec3 frost_color = vec3(0.98, 0.99, 1.0);
    tinted = mix(tinted, frost_color, frost_pattern * 0.12);

    float micro_frost = fractal_noise(coords * size * 0.02, 2) * frost_pattern;
    tinted += vec3(micro_frost * 0.06);

    FragColor = vec4(tinted, original.a);
}