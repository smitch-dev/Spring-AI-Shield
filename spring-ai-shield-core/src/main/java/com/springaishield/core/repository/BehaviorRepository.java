package com.springaishield.core.repository;

import com.springaishield.core.model.UserBehavior;
import java.util.List;

public interface BehaviorRepository {


    UserBehavior save(UserBehavior behavior);

    List<UserBehavior> findRecentByUserId(String userId, int limit);
}