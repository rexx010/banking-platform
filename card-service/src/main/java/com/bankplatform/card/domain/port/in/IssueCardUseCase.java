package com.bankplatform.card.domain.port.in;

import com.bankplatform.card.application.usecase.CardCommands.IssueCardCommand;
import com.bankplatform.card.domain.model.Card;

public interface IssueCardUseCase {
    Card issueCard(IssueCardCommand command);
}