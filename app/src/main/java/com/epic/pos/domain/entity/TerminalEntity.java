package com.epic.pos.domain.entity;

import com.epic.pos.data.db.dbpos.modal.Terminal;

public class TerminalEntity {

    private Terminal terminal;

    public TerminalEntity(Terminal terminal) {
        this.terminal = terminal;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }
}
