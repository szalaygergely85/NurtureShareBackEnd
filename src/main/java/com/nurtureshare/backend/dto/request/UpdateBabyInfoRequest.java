package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.BabyGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBabyInfoRequest {
    private BabyGender babyGender;
    private String babyName;
}
