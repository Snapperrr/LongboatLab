#version 150
#moj_import <fog.glsl>
#moj_import <light.glsl>
uniform sampler2D PatchMask;
uniform sampler2D PatchLight;
uniform sampler2D Sampler2;
uniform sampler2D WaterAtlas;
uniform vec4 WaterStill;
uniform float WaterUvInset;
uniform float UpShade;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
in vec2 localXZ;
in vec3 waterNormal;
in float vertexDistance;
out vec4 fragColor;
void main() {
    ivec2 cellPos = clamp(ivec2(floor(localXZ)), ivec2(0), ivec2(47));
    vec4 cell = texelFetch(PatchMask, cellPos, 0);
    if (cell.a < 0.5) discard;
    // Vanilla still water repeats once PER BLOCK, with the same atlas seam inset.
    // Differentiate the unwrapped coordinate to avoid mip seams at fract boundaries.
    vec2 tile = mix(vec2(WaterUvInset), vec2(1.0 - WaterUvInset), fract(localXZ));
    vec2 scale = (WaterStill.zw - WaterStill.xy) * (1.0 - 2.0 * WaterUvInset);
    vec4 water = textureGrad(WaterAtlas, mix(WaterStill.xy, WaterStill.zw, tile),
                             dFdx(localXZ) * scale, dFdy(localXZ) * scale);
    ivec2 light = ivec2(round(texelFetch(PatchLight, cellPos, 0).rg * 255.0));
    vec4 color = water * vec4(cell.rgb * UpShade, 1.0)
                 * minecraft_sample_lightmap(Sampler2, light) * ColorModulator;
    // Geometry carries the depression/parallax. Restrained, symmetric slope shading only;
    // a flat node is exactly vanilla material, without a separate reflective blue patch.
    float slopeShade = 1.0 - 0.12 * (1.0 - clamp(normalize(waterNormal).y, 0.0, 1.0));
    color.rgb *= slopeShade;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
