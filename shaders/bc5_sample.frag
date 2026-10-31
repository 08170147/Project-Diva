#version 450

layout(set = 0, binding = 0) uniform sampler2D bc5Texture;
layout(location = 0) in vec2 vUV;
layout(location = 0) out vec4 outColor;

vec3 checker(vec2 uv) {
    float c = mod(
        floor(uv.x * 16.0) +
        floor(uv.y * 16.0),
        2.0
    );

    return mix(vec3(0.12), vec3(0.72), c);
}

float gridLine(float v, float count, float width) {
    float p = fract(v * count);
    return 1.0 - step(
        width,
        min(p, 1.0 - p)
    );
}

void main() {
    vec2 uv = vUV;

    // Sample original BC5 channels
    vec2 rg = texture(bc5Texture, uv).rg;

    float r = rg.r;
    float g = rg.g;

    // Reconstruct grayscale + positive G mask
    vec3 bg = checker(uv);
    vec3 result = mix(bg, vec3(r), g);

    // Minor 16x16 grid
    float minorGrid = max(
        gridLine(uv.x, 16.0, 0.018),
        gridLine(uv.y, 16.0, 0.018)
    );

    // Major 4x4 subdivision
    float majorGrid = max(
        gridLine(uv.x, 4.0, 0.025),
        gridLine(uv.y, 4.0, 0.025)
    );

    result = mix(
        result,
        vec3(0.0, 0.75, 1.0),
        minorGrid * 0.35
    );

    result = mix(
        result,
        vec3(1.0, 0.2, 0.0),
        majorGrid * 0.85
    );

    outColor = vec4(result, 1.0);
}
