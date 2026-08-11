package com.bankplatform.card.domain.port.in;

import com.bankplatform.card.application.usecase.CardCommands.CardTransactionCommand;
import com.bankplatform.card.domain.model.Card;

public interface ProcessCardTransactionUseCase {
    Card processTransaction(CardTransactionCommand command);
}