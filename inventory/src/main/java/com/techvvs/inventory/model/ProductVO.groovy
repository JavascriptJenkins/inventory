package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.*
import java.time.LocalDateTime;

@JsonIgnoreProperties
@Entity
@Table(name="product")
class ProductVO implements Serializable, Comparable<ProductVO> {
// todo: make a productsale object
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer product_id;
    @ManyToOne
    @JoinColumn(name = "batchid")
    BatchVO batch;
    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="producttypeid")
    ProductTypeVO producttypeid;
    @JsonProperty
    @ManyToMany(mappedBy = "product_cart_list")
    List<CartVO> cart_list
    @JsonProperty
    @ManyToMany(mappedBy = "product_list")
    List<TransactionVO> transaction_list
    @JsonProperty
    @ManyToMany(mappedBy = "product_package_list")
    List<PackageVO> package_list
    @JsonProperty
    @ManyToMany(mappedBy = "menu_product_list")
    List<MenuVO> menu_list
    @JsonProperty
    Integer productnumber;
    @JsonProperty
    Integer quantity; // this is the original quantity in the batch
    @JsonProperty
    Integer vendorquantity; // quantity claimed by vendor
    @JsonProperty
    Integer quantityremaining;
    @JsonProperty
    String notes
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    String vendor;
    @JsonProperty
    String bagcolor;
    @JsonProperty
    Integer crate;
    @JsonProperty
    String crateposition;
    @JsonProperty
    String barcode;
    @JsonProperty
    Double price;
    @JsonProperty
    Double cost;
    @JsonProperty
    Integer salePrice;
    @JsonProperty
    Integer laborCostPricePerUnit;
    @JsonProperty
    Integer marginPercent;
    @JsonProperty
    Double weight
    @JsonProperty
    String unitofmeasure;

    // for display purposes only
    @Transient
    Integer displayquantity

    // for display purposes only
    @Transient
    String primaryphoto

    // for display purposes only
    @Transient
    String videodir

    // for display purposes only
    @Transient
    Double displayprice

    // for display purposes only
    @Transient
    Integer displayquantityReturned

    // for adding Product to the Cart
    @Transient
    Integer quantityselected

    @Transient
    String batchname

    @Transient
    Integer batch_type_id

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


    @Override
    public int compareTo(ProductVO other) {
        // Example comparison based on price
        return Double.compare(this.price, other.price);
    }


    // Method to get unique products based on barcode
    // Method to get unique products based on barcode and return as a list
    static List<ProductVO> getUniqueProducts(List<ProductVO> products) {
        Set<String> seenBarcodes = new HashSet<>();
        List<ProductVO> uniqueProducts = new ArrayList<>();
        for (ProductVO product : products) {
            if (seenBarcodes.add(product.getBarcode())) {
                uniqueProducts.add(product);
            }
        }
        return uniqueProducts;
    }


    public static void sortProductsByPrice(ArrayList<ProductVO> listofproductsinstock) {
        Collections.sort(listofproductsinstock, new Comparator<ProductVO>() {
            @Override
            public int compare(ProductVO p1, ProductVO p2) {
                return Double.compare(p1.getPrice(), p2.getPrice());
            }
        });
    }

    public static void sortProductsByDisplayPrice(ArrayList<ProductVO> listofproductsinstock) {
        Collections.sort(listofproductsinstock, new Comparator<ProductVO>() {
            @Override
            public int compare(ProductVO p1, ProductVO p2) {
                return Double.compare(p1.getDisplayprice(), p2.getDisplayprice());
            }
        });
    }

    }
