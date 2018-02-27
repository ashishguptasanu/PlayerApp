package live.player.edge.com.playerapp.Models;

/**
 * Created by Ashish on 27-02-2018.
 */

public class Winners {
    private String userId;
    private String ammount;
    private String photoUrl;
    private String userName;

    public String getUserId() {
        return userId;
    }

    public String getAmmount() {
        return ammount;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getUserName() {
        return userName;
    }



    public Winners(String userId, String ammount, String photoUrl, String userName) {
        this.userId = userId;
        this.ammount = ammount;
        this.photoUrl = photoUrl;
        this.userName = userName;
    }


}
