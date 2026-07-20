package com.bankplatform.identity.domain.port.in;

import com.bankplatform.identity.application.usecase.IdentityCommands.*;
import com.bankplatform.identity.domain.model.Customer;

public interface CreateBvnUseCase {
    Customer createBvn(CreateBvnCommand command);
}
