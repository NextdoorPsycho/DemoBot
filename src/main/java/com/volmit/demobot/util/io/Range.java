
package com.volmit.demobot.util.io;


import art.arcane.quill.random.RNG;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true, fluent = true)
public class Range {

    @Builder.Default
    private float min = 0;
    @Builder.Default
    private float max = 1;
    public static Range jitter(float center, float jitter) {
        return new Range(center - (jitter / 2), center + (jitter / 2));
    }

    public float rand() {
        return RNG.r.f(min, max);
    }
    public boolean contains(float d) {
        return d >= min && d <= max;
    }
}


