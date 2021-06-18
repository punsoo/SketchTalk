package messenger_project.sketchtalk.main;

public class ActivityCommunicator {
    public interface MainAcitivityListener {
        void notifyDataSetChanged();
    }

    private static ActivityCommunicator mInstance;
    private MainAcitivityListener mListener;

    private ActivityCommunicator() {}

    public static ActivityCommunicator getInstance() {
        if(mInstance == null) {
            mInstance = new ActivityCommunicator();
        }
        return mInstance;
    }

    public void setListener(MainAcitivityListener listener) {
        mListener = listener;
    }

    public void setOffListener() {
        mListener = null;
    }

    public void notifyDataSetChanged() {
        if(mListener != null) {
            mListener.notifyDataSetChanged();
        }
    }
}
