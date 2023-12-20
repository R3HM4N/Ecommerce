package az.crocusoft.ecommerce.service.Impl;

import az.crocusoft.ecommerce.dto.OrderDto;
import az.crocusoft.ecommerce.dto.cart.CartDto;
import az.crocusoft.ecommerce.exception.CartNotFoundException;
import az.crocusoft.ecommerce.exception.UserNotFoundException;
import az.crocusoft.ecommerce.model.*;
import az.crocusoft.ecommerce.repository.CartRepository;
import az.crocusoft.ecommerce.repository.OrderItemRepository;
import az.crocusoft.ecommerce.repository.OrderRepository;
import az.crocusoft.ecommerce.repository.UserRepository;
import az.crocusoft.ecommerce.service.CartService;
import az.crocusoft.ecommerce.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {



    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    private final ModelMapper modelMapper;
    private final CartService cartService;
    private final OrderItemRepository  orderItemRepository;




//    @Override
//    public Order placeOrder(OrderDto orderDto, Long userId) {
//
//        Optional<User> user = userRepository.findById(userId);
//        CartDto cartDto = cartService.listCartItems(user.get());
//        Cart cart = cartRepository.findByUser(user.get());
//        if (cart==null){
//            throw new CartNotFoundException("Cart not found ", "cartId", userId);
//        }
//        Order order = modelMapper.map(orderDto, Order.class);
//        order.setOrderDate(LocalDate.now());
//        OrderItem orderItem = new OrderItem();
//        orderItem.setOrder(order);
//        orderItem.setQuantity(cart.getQuantity());
//        orderItem.setTotalAmount(cartDto.getTotalPrice());
//        orderItem.setProduct(cart.getProductVariation().getProduct());
//        order.setCart(cart);
//        order.setUser(user.get());
//        orderItemRepository.save(orderItem);
//        order.setOrderStatus(OrderStatusValues.PENDING);
//        Order savedOrder = orderRepository.save(order);
//        return savedOrder ;
//    }
public Order placeOrder(OrderDto orderDto, Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow();

    Cart cart = cartRepository.findByUser(user);
    if (cart == null) {
        throw new CartNotFoundException("Cart not found", "cartId", userId);
    }

    Order order = createOrderFromDto(orderDto, user, cart);
    OrderItem orderItem = createOrderItem(order, cart);

    orderItemRepository.save(orderItem);
    order.setOrderStatus(OrderStatusValues.PENDING);

    return orderRepository.save(order);
}

    private Order createOrderFromDto(OrderDto orderDto, User user, Cart cart) {
        Order order = modelMapper.map(orderDto, Order.class);
        order.setOrderDate(LocalDate.now());
        order.setCart(cart);
        order.setUser(user);
        return order;
    }

    private OrderItem createOrderItem(Order order, Cart cart) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setQuantity(cart.getQuantity());
        orderItem.setTotalAmount(cart.getTotalPrice());
        orderItem.setProduct(cart.getProductVariation().getProduct());
        return orderItem;
    }


    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }


    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional
    public OrderDto updateOrder(Long orderId, OrderDto orderDto) {

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new CartNotFoundException("Order", "orderId", orderId));

        modelMapper.map(orderDto, existingOrder);
        Order updatedOrder = orderRepository.save(existingOrder);
        return modelMapper.map(updatedOrder, OrderDto.class);
    }
    @Override
    public List<OrderDto> getOrdersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Order> userOrders = user.getOrders();
        return userOrders.stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }
 }