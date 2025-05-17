package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MutualMarkPairWeightDto implements MutualLikePairWeight {

    private long userA;
    private long userB;
    private long weight;

    @Override
    public long getUserA() {
        return userA;
    }

    @Override
    public long getUserB() {
        return userB;
    }

    @Override
    public long getWeight() {
        return weight;
    }
}
