package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.AppMode;
import lombok.Data;

@Data
public class UpdateSettingsRequest {

    private Boolean notificationsEnabled;
    private AppMode appMode;
}
