package com.princz_mia.viaual04_gourmetgo_backend.events;

import com.princz_mia.viaual04_gourmetgo_backend.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {

    private User user;
    private EventType type;
    private Map<?, ?> data;
}
