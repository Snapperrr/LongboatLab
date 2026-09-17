#version 150
#moj_import <fog.glsl>
uniform sampler2D Sampler0;
uniform sampler2D PatchMask;
uniform vec3 PatchOrigin;
uniform vec4 WaterStill;
uniform vec4 WaterFlow;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 relativePosition;
out vec4 fragColor;
bool inSprite(vec4 rect) {
    return all(greaterThanEqual(texCoord0, rect.xy)) && all(lessThanEqual(texCoord0, rect.zw));
}
void main() {
    vec3 local = relativePosition - PatchOrigin;
    // Derivatives are evaluated before divergence. Side faces and glass keep vanilla rendering.
    vec3 face = cross(dFdx(relativePosition), dFdy(relativePosition));
    bool top = abs(face.y) > length(face) * 0.95;
    if (top && abs(local.y) < 0.08 && (inSprite(WaterStill) || inSprite(WaterFlow))
            && all(greaterThanEqual(local.xz, vec2(0.0))) && all(lessThan(local.xz, vec2(48.0)))) {
        if (texture(PatchMask, local.xz / 48.0).a > 0.5) discard;
    }
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
