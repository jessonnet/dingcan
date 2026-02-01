package com.canteen.dto;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AggregatedOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate orderDate;

    private List<OrderDetail> orders;

    private Integer totalCount;

    private BigDecimal totalAmount;

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderDetail> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDetail> orders) {
        this.orders = orders;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public static class OrderDetail implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        private Long mealTypeId;

        private String mealTypeName;

        private BigDecimal price;

        private Integer status;

        private String statusText;

        private String createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getMealTypeId() {
            return mealTypeId;
        }

        public void setMealTypeId(Long mealTypeId) {
            this.mealTypeId = mealTypeId;
        }

        public String getMealTypeName() {
            return mealTypeName;
        }

        public void setMealTypeName(String mealTypeName) {
            this.mealTypeName = mealTypeName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
