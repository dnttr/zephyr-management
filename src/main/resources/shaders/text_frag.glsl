#version 330 core

out vec4 FragColor;

in vec2 tex_coords;

flat in int character_index;
flat in int characters_amount;

layout (std140) uniform text_properties
{
    float time;
    float smoothing;
    vec2 text_position;
    vec4 text_color;

    int text_shadow_enable;
    vec4 text_shadow_color;
    vec2 text_shadow_offset;

    int text_outline_enable;
    vec4 text_outline_color;
    float text_outline_width;

    int text_glow_enable;
    float text_glow_radius;
    float text_glow_intensity;
    vec4 text_glow_color;

    int text_rainbow_enable;
    float text_rainbow_speed;
    float text_rainbow_variation;
    float text_rainbow_saturation;
};

uniform sampler2D atlas;

vec3 hsv2rgb(vec3 hsv_color) { // i have no damn idea how this works, lets just leave it
    vec4 hue_offsets = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 hue_positions = abs(fract(hsv_color.xxx + hue_offsets.xyz) * 6.0 - hue_offsets.www);

    return hsv_color.z * mix(vec3(1.0), clamp(hue_positions - vec3(1.0), 0.0, 1.0), hsv_color.y);
}

vec4 apply_shadow(vec4 current_color, float adjusted_smoothing) {
    float show_distance = texture(atlas, tex_coords - text_shadow_offset / textureSize(atlas, 0)).r;
    float show_alpha = smoothstep(0.5 - adjusted_smoothing, 0.5 + adjusted_smoothing, show_distance) * text_shadow_color.a;

    return mix(current_color, text_shadow_color, show_alpha);
}

vec4 apply_glow(vec4 current_color, float text_alpha, float distance) {
    float glowDist = smoothstep(0.5 - text_glow_radius, 0.5, distance);
    float glow = glowDist * (1.0 - text_alpha) * text_glow_intensity;

    vec4 result = current_color;

    result.rgb = mix(result.rgb, text_glow_color.rgb, glow * text_glow_color.a);
    result.a = max(result.a, glow * text_glow_color.a);

    return result;
}

vec4 apply_outline(vec4 current_color, float text_alpha, float adjusted_smoothing) {
    float outline = 0.0;
    int samples = 16;

    float step_size = text_outline_width / float(samples);
    vec2 tex_size = vec2(textureSize(atlas, 0));

    for (int i = 1; i <= samples; i++) {
        float radius = step_size * float(i);

        for (int j = 0; j < 16; j++) {
            float angle = 6.2831853 * float(j) / 16.0;
            vec2 offset = vec2(cos(angle), sin(angle)) * radius / tex_size;

            float val = texture(atlas, tex_coords + offset).r;
            outline = max(outline, val);
        }
    }

    float outline_alpha = smoothstep(0.5 - adjusted_smoothing, 0.5 + adjusted_smoothing, outline) * text_outline_color.a;
    outline_alpha = min(outline_alpha, 1.0 - text_alpha);

    return mix(current_color, text_outline_color, outline_alpha);
}

vec4 apply_rainbow(vec4 current_color) {
    float baseHue = float(character_index) / float(characters_amount);
    float shift = tex_coords.x;

    float hue = fract(baseHue + (text_position.x / 100) * shift * text_rainbow_variation + time * text_rainbow_speed);

    vec3 result = hsv2rgb(vec3(hue, text_rainbow_saturation, 1.0));
    return vec4(result, current_color.a);
}

void main()
{
    float distance = texture(atlas, tex_coords).r;
    float adjusted_smoothing = fwidth(distance);
    float text_alpha = smoothstep(0.5 - adjusted_smoothing, 0.5 + adjusted_smoothing, distance);

    vec4 final_color = vec4(0.0);

    if (text_shadow_enable != 0) {
        final_color = apply_shadow(final_color, adjusted_smoothing);
    }

    if (text_glow_enable != 0) {
        final_color = apply_glow(final_color, text_alpha, distance);
    }

    if (text_outline_enable != 0) {
        final_color = apply_outline(final_color, text_alpha, adjusted_smoothing);
    }

    vec4 final_text_color = text_color;
    if (text_rainbow_enable != 0) {
        final_text_color = apply_rainbow(text_color);
    }

    final_color = mix(final_color, final_text_color, text_alpha);

    if (final_color.a < 0.01) {
        discard;
    }

    FragColor = final_color;
}