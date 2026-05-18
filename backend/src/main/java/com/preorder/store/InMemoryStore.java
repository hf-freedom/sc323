package com.preorder.store;

import com.preorder.entity.PreorderOrder;
import com.preorder.entity.PreorderProduct;
import com.preorder.entity.User;
import com.preorder.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryStore {

    public static final ConcurrentHashMap<Long, PreorderProduct> PRODUCT_STORE = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, PreorderOrder> ORDER_STORE = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Long, User> USER_STORE = new ConcurrentHashMap<>();

    public static final AtomicLong PRODUCT_ID_GENERATOR = new AtomicLong(1);
    public static final AtomicLong ORDER_ID_GENERATOR = new AtomicLong(1);
    public static final AtomicLong USER_ID_GENERATOR = new AtomicLong(1);

    static {
        initTestData();
    }

    private static void initTestData() {
        User user1 = new User();
        user1.setId(USER_ID_GENERATOR.getAndIncrement());
        user1.setUsername("user001");
        user1.setNickname("测试用户001");
        user1.setBalance(new BigDecimal("10000.00"));
        user1.setCreateTime(LocalDateTime.now());
        USER_STORE.put(user1.getId(), user1);

        User user2 = new User();
        user2.setId(USER_ID_GENERATOR.getAndIncrement());
        user2.setUsername("user002");
        user2.setNickname("测试用户002");
        user2.setBalance(new BigDecimal("5000.00"));
        user2.setCreateTime(LocalDateTime.now());
        USER_STORE.put(user2.getId(), user2);

        LocalDateTime now = LocalDateTime.now();

        PreorderProduct p1 = new PreorderProduct();
        p1.setId(PRODUCT_ID_GENERATOR.getAndIncrement());
        p1.setName("新款智能手机 Pro Max");
        p1.setDescription("最新旗舰手机，512GB大存储，6.7英寸屏幕");
        p1.setImage("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400");
        p1.setOriginalPrice(new BigDecimal("8999.00"));
        p1.setPreorderPrice(new BigDecimal("7999.00"));
        p1.setDeposit(new BigDecimal("1000.00"));
        p1.setTotalStock(100);
        p1.setLockedStock(0);
        p1.setSoldStock(0);
        p1.setDepositStartTime(now.minusDays(1));
        p1.setDepositEndTime(now.plusDays(2));
        p1.setBalanceStartTime(now.plusDays(2));
        p1.setBalanceEndTime(now.plusDays(5));
        p1.setActive(true);
        p1.setDepositRule("定金1000元可抵扣2000元，尾款期开始后支付尾款");
        p1.setCreateTime(now);
        p1.setUpdateTime(now);
        PRODUCT_STORE.put(p1.getId(), p1);

        PreorderProduct p2 = new PreorderProduct();
        p2.setId(PRODUCT_ID_GENERATOR.getAndIncrement());
        p2.setName("高端无线降噪耳机");
        p2.setDescription("主动降噪，40小时续航，蓝牙5.3");
        p2.setImage("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400");
        p2.setOriginalPrice(new BigDecimal("1999.00"));
        p2.setPreorderPrice(new BigDecimal("1499.00"));
        p2.setDeposit(new BigDecimal("200.00"));
        p2.setTotalStock(200);
        p2.setLockedStock(0);
        p2.setSoldStock(0);
        p2.setDepositStartTime(now);
        p2.setDepositEndTime(now.plusDays(3));
        p2.setBalanceStartTime(now.plusDays(3));
        p2.setBalanceEndTime(now.plusDays(7));
        p2.setActive(true);
        p2.setDepositRule("定金200元可抵扣400元");
        p2.setCreateTime(now);
        p2.setUpdateTime(now);
        PRODUCT_STORE.put(p2.getId(), p2);

        PreorderProduct p3 = new PreorderProduct();
        p3.setId(PRODUCT_ID_GENERATOR.getAndIncrement());
        p3.setName("智能手表 Ultra");
        p3.setDescription("健康监测，GPS定位，防水50米");
        p3.setImage("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400");
        p3.setOriginalPrice(new BigDecimal("2599.00"));
        p3.setPreorderPrice(new BigDecimal("2199.00"));
        p3.setDeposit(new BigDecimal("300.00"));
        p3.setTotalStock(150);
        p3.setLockedStock(1);
        p3.setSoldStock(0);
        p3.setDepositStartTime(now.minusDays(2));
        p3.setDepositEndTime(now.minusHours(1));
        p3.setBalanceStartTime(now.minusHours(1));
        p3.setBalanceEndTime(now.plusDays(3));
        p3.setActive(true);
        p3.setDepositRule("定金300元可抵扣500元，限时特惠");
        p3.setCreateTime(now);
        p3.setUpdateTime(now);
        PRODUCT_STORE.put(p3.getId(), p3);

        PreorderOrder testOrder = new PreorderOrder();
        testOrder.setId(ORDER_ID_GENERATOR.getAndIncrement());
        testOrder.setOrderNo("PO" + System.currentTimeMillis() + "TEST01");
        testOrder.setUserId(1L);
        testOrder.setUserName("测试用户001");
        testOrder.setProductId(p3.getId());
        testOrder.setProductName(p3.getName());
        testOrder.setProductImage(p3.getImage());
        testOrder.setQuantity(1);
        testOrder.setOriginalPrice(p3.getOriginalPrice());
        testOrder.setPreorderPrice(p3.getPreorderPrice());
        testOrder.setDeposit(p3.getDeposit());
        testOrder.setBalance(p3.getPreorderPrice().subtract(p3.getDeposit()));
        testOrder.setTotalAmount(p3.getPreorderPrice());
        testOrder.setDepositPaid(p3.getDeposit());
        testOrder.setBalancePaid(BigDecimal.ZERO);
        testOrder.setRefundAmount(BigDecimal.ZERO);
        testOrder.setStatus(OrderStatus.BALANCE_PERIOD);
        testOrder.setDepositPayTime(now.minusDays(1));
        testOrder.setBalancePayTime(null);
        testOrder.setDepositStartTime(p3.getDepositStartTime());
        testOrder.setDepositEndTime(p3.getDepositEndTime());
        testOrder.setBalanceStartTime(p3.getBalanceStartTime());
        testOrder.setBalanceEndTime(p3.getBalanceEndTime());
        testOrder.setCreateTime(now.minusDays(2));
        testOrder.setUpdateTime(now);
        ORDER_STORE.put(testOrder.getOrderNo(), testOrder);
    }

    public static List<PreorderProduct> getAllProducts() {
        return new ArrayList<>(PRODUCT_STORE.values());
    }

    public static PreorderProduct getProduct(Long id) {
        return PRODUCT_STORE.get(id);
    }

    public static void saveProduct(PreorderProduct product) {
        product.setUpdateTime(LocalDateTime.now());
        PRODUCT_STORE.put(product.getId(), product);
    }

    public static void saveOrder(PreorderOrder order) {
        order.setUpdateTime(LocalDateTime.now());
        ORDER_STORE.put(order.getOrderNo(), order);
    }

    public static PreorderOrder getOrder(String orderNo) {
        return ORDER_STORE.get(orderNo);
    }

    public static List<PreorderOrder> getAllOrders() {
        return new ArrayList<>(ORDER_STORE.values());
    }

    public static User getUser(Long id) {
        return USER_STORE.get(id);
    }
}
