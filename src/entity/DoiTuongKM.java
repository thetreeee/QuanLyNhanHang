package entity;

public enum DoiTuongKM {
    TAT_CA("Tất cả khách hàng"),
    TRUA("Khách dùng bữa trưa"),
    MON("Khách dùng món");

    private final String label;

    DoiTuongKM(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static DoiTuongKM fromLabel(String label) {
        if (label == null) return null;
        for (DoiTuongKM dt : DoiTuongKM.values()) {
            if (dt.getLabel().equals(label.trim())) {
                return dt;
            }
        }
        return null;
    }
}