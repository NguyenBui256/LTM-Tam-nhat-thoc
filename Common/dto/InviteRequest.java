package dto;

import java.io.Serializable;

public class InviteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String inviter;
    private String invited;

    public InviteRequest(String inviter, String invited) {
        this.inviter = inviter;
        this.invited = invited;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public String getInvited() {
        return invited;
    }

    public void setInvited(String invited) {
        this.invited = invited;
    }
}
