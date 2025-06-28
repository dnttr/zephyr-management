#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D fontAtlas;
uniform vec4 textColor;
uniform vec4 outlineColor;
uniform vec4 shadowColor;
uniform float smoothing;
uniform float outlineWidth;
uniform vec2 shadowOffset;
uniform int useEffects;
uniform int totalChars;
uniform float variation;
uniform vec2 position;
uniform float saturation;
uniform float time;
uniform float speed;

uniform int useGlow;
uniform float glowRadius;
uniform float glowIntensity;
uniform vec4 glowColor;

flat in float revealAlpha;
flat in int charIndex;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1., 2./3., 1./3., 3.);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6. - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0., 1.), c.y);
}

void main()
{
    float distance = texture(fontAtlas, TexCoords).r;
    float textAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);

    vec4 finalColor = vec4(0.0);

    if (shadowOffset != vec2(0.0, 0.0)) {
        float shadowDistance = texture(fontAtlas, TexCoords - shadowOffset / textureSize(fontAtlas, 0)).r;
        float shadowAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, shadowDistance) * shadowColor.a;
        finalColor = mix(finalColor, shadowColor, shadowAlpha);
    }

    if (useGlow != 0 && glowRadius > 0.0) {
        float glowDist = smoothstep(0.5 - glowRadius, 0.5, distance);
        float glow = glowDist * (1.0 - textAlpha) * glowIntensity;

        finalColor.rgb = mix(finalColor.rgb, glowColor.rgb, glow * glowColor.a);
        finalColor.a = max(finalColor.a, glow * glowColor.a);
    }

    if (outlineWidth > 0.0) {
        float outline = 0.0;
        int samples = 16;
        float stepSize = outlineWidth / float(samples);
        vec2 texSize = vec2(textureSize(fontAtlas, 0));

        for (int i = 1; i <= samples; i++) {
            float radius = stepSize * float(i);

            for (int j = 0; j < 16; j++) {
                float angle = 6.2831853 * float(j) / 16.0;
                vec2 offset = vec2(cos(angle), sin(angle)) * radius / texSize;
                float sampleValue = texture(fontAtlas, TexCoords + offset).r;
                outline = max(outline, sampleValue);
            }
        }

        float outlineAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, outline) * outlineColor.a;
        outlineAlpha = min(outlineAlpha, 1.0 - textAlpha);

        finalColor = mix(finalColor, outlineColor, outlineAlpha);
    }

    vec4 effectTextColor = textColor;
    if (useEffects != 0) {
        float baseHue = float(charIndex) / float(totalChars);
        float shift = TexCoords.x;
        float hue = fract(baseHue + (position.x / 100) * shift * variation + time * speed);
        vec3 rainbowColor = hsv2rgb(vec3(hue, saturation, 1.0));
        effectTextColor = vec4(rainbowColor, 1.0);
    }

    finalColor = mix(finalColor, effectTextColor, textAlpha);

    finalColor.a *= revealAlpha;

    if (finalColor.a < 0.01) {
        discard;
    }

    FragColor = finalColor;
}