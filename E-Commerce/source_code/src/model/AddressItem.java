package model;
public class AddressItem {
    public Long addressId;
    public String displayLabel;

    public AddressItem(Long addressId, String displayLabel) {
        this.addressId = addressId;
        this.displayLabel = displayLabel;
    }

    @Override
    public String toString() {
        return displayLabel; // Quy định cách hiển thị trên ComboBox
    }
}
