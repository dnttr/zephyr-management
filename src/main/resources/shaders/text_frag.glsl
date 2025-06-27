#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D fontAtlas;
uniform vec4 textColor;
uniform vec4 outlineColor = vec4(0.0, 0.0, 0.0, 0.5);
uniform vec4 shadowColor = vec4(0.0, 0.0, 0.0, 0.5);
uniform float smoothing = 0.1;
uniform float outlineWidth = 0.0;  // Set to 0 to disable outline
uniform vec2 shadowOffset = vec2(0.0, 0.0);  // Set to 0,0 to disable shadow

void main()
{
    float distance = texture(fontAtlas, TexCoords).r;

    if (distance < 0.01 && outlineWidth == 0.0 && shadowOffset == vec2(0.0, 0.0))
        discard;

    float textAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);

    vec4 finalColor = vec4(0.0);

    if (shadowOffset != vec2(0.0, 0.0)) {
        float shadowDistance = texture(fontAtlas, TexCoords - shadowOffset / textureSize(fontAtlas, 0)).r;
        float shadowAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, shadowDistance) * shadowColor.a;
        finalColor = mix(finalColor, shadowColor, shadowAlpha);
    }

    if (outlineWidth > 0.0) {
        float outline = 0.0;
        const int samples = 4;
        const float step = outlineWidth / float(samples);

        for (int i = 1; i <= samples; i++) {
            float w = step * float(i);
            outline = max(outline, texture(fontAtlas, TexCoords + vec2(w, 0.0)).r);
            outline = max(outline, texture(fontAtlas, TexCoords + vec2(-w, 0.0)).r);
            outline = max(outline, texture(fontAtlas, TexCoords + vec2(0.0, w)).r);
            outline = max(outline, texture(fontAtlas, TexCoords + vec2(0.0, -w)).r);
        }

        float outlineAlpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, outline) * outlineColor.a;
        outlineAlpha = min(outlineAlpha, 1.0 - textAlpha); // Prevent outline from drawing over text

        finalColor = mix(finalColor, outlineColor, outlineAlpha);
    }

    finalColor = mix(finalColor, textColor, textAlpha);

    if (finalColor.a < 0.01)
        discard;

    FragColor = finalColor;
}