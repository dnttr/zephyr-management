#version 330 core

out vec4 FragColor;

in vec2 tex_coords;

flat in int character_index;
flat in int characters_amount;

uniform float time;
uniform float smoothing;
uniform sampler2D atlas;

uniform vec2 text_position;
uniform vec4 text_color;

uniform int text_shadow_enable;
uniform vec4 text_shadow_color;
uniform vec2 text_shadow_offset;

uniform int text_outline_enable;
uniform vec4 text_outline_color;
uniform float text_outline_width;

uniform int text_glow_enable;
uniform float text_glow_radius;
uniform float text_glow_intensity;
uniform vec4 text_glow_color;

uniform int text_rainbow_enable;
uniform float text_rainbow_speed;
uniform float text_rainbow_variation;
uniform float text_rainbow_saturation;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1., 2./3., 1./3., 3.);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6. - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0., 1.), c.y);
}

void main()
{
    float distance = texture(atlas, tex_coords).r;
    float textAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);

    vec4 finalColor = vec4(0.0);

    if (text_shadow_enable != 0) {
        float shadowDistance = texture(atlas, tex_coords - text_shadow_offset / textureSize(atlas, 0)).r;
        float shadowAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, shadowDistance) * text_shadow_color.a;
        finalColor = mix(finalColor, text_shadow_color, shadowAlpha);
    }

    if (text_glow_enable != 0 && text_glow_radius > 0.0) {
        float glowDist = smoothstep(0.5 - text_glow_radius, 0.5, distance);
        float glow = glowDist * (1.0 - textAlpha) * text_glow_intensity;

        finalColor.rgb = mix(finalColor.rgb, text_glow_color.rgb, glow * text_glow_color.a);
        finalColor.a = max(finalColor.a, glow * text_glow_color.a);
    }

    if (text_outline_enable != 0) {
        float outline = 0.0;
        int samples = 16;
        float stepSize = text_outline_width / float(samples);
        vec2 texSize = vec2(textureSize(atlas, 0));

        for (int i = 1; i <= samples; i++) {
            float radius = stepSize * float(i);

            for (int j = 0; j < 16; j++) {
                float angle = 6.2831853 * float(j) / 16.0;
                vec2 offset = vec2(cos(angle), sin(angle)) * radius / texSize;
                float sampleValue = texture(atlas, tex_coords + offset).r;
                outline = max(outline, sampleValue);
            }
        }

        float outlineAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, outline) * text_outline_color.a;
        outlineAlpha = min(outlineAlpha, 1.0 - textAlpha);

        finalColor = mix(finalColor, text_outline_color, outlineAlpha);
    }

    vec4 effectTextColor = text_color;
    if (text_rainbow_enable != 0) {
        float baseHue = float(character_index) / float(characters_amount);
        float shift = tex_coords.x;
        float hue = fract(baseHue + (text_position.x / 100) * shift * text_rainbow_variation + time * text_rainbow_speed);
        vec3 rainbowColor = hsv2rgb(vec3(hue, text_rainbow_saturation, 1.0));
        effectTextColor = vec4(rainbowColor, 1.0);
    }

    finalColor = mix(finalColor, effectTextColor, textAlpha);

    if (finalColor.a < 0.01) {
        discard;
    }

    FragColor = finalColor;
}