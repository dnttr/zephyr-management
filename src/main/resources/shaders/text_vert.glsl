#version 330 core

layout (location = 0) in vec2 r_text_position;
layout (location = 1) in vec2 r_tex_coords;
layout(location = 2) in float r_character_index;

out vec2 tex_coords;

flat out int character_index;
flat out int characters_amount;

flat out float f_scale;

layout (std140) uniform transform_properties
{
    mat4 projection_matrix;
    float time;
    int total_characters_amount;
    float text_animation_begin;
    float text_animation_end;
    float text_animation_speed;


    float text_magnification;
};

///todo: reveal animation
void main()
{
    character_index = int(r_character_index);
    characters_amount = total_characters_amount;

    float scale = min(text_animation_end, text_animation_begin + time * text_animation_speed);

    vec2 final_position = r_text_position * scale * text_magnification;
    gl_Position = projection_matrix * vec4(final_position, 0.0, 1.0);

    tex_coords = r_tex_coords;
}
