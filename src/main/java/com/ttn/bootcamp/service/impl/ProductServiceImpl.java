package com.ttn.bootcamp.service.impl;

import com.google.gson.Gson;
import com.ttn.bootcamp.ApplicationConstants;
import com.ttn.bootcamp.domains.Product.Category;
import com.ttn.bootcamp.domains.Product.Product;
import com.ttn.bootcamp.domains.User.Seller;
import com.ttn.bootcamp.dto.Product.ProductDto;
import com.ttn.bootcamp.dto.User.SellerDto;
import com.ttn.bootcamp.exceptions.GenericException;
import com.ttn.bootcamp.repository.CategoryRepository;
import com.ttn.bootcamp.repository.ProductRepository;
import com.ttn.bootcamp.repository.ProductVariationRepository;
import com.ttn.bootcamp.repository.SellerRepository;
import com.ttn.bootcamp.security.AppUser;
import com.ttn.bootcamp.service.EmailService;
import com.ttn.bootcamp.service.ProductService;
import com.ttn.bootcamp.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private ProductRepository productRepository;
    private SellerService sellerService;
    private CategoryRepository categoryRepository;
    private EmailService emailService;
    private SellerRepository sellerRepository;
    private ProductVariationRepository productVariationRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, SellerService sellerService, CategoryRepository categoryRepository, EmailService emailService, SellerRepository sellerRepository, ProductVariationRepository productVariationRepository) {
        this.productRepository = productRepository;
        this.sellerService = sellerService;
        this.categoryRepository = categoryRepository;
        this.emailService = emailService;
        this.sellerRepository = sellerRepository;
        this.productVariationRepository = productVariationRepository;
    }

    @Value("${user.admin.email}")
    private String adminEmail;

    private void checkForProductExist(String name, String brand, Category category, Seller seller) throws GenericException {

        if (productRepository.findByNameAndBrandAndCategoryAndSeller(name, brand, category, seller).isPresent())
            throw new GenericException("Product already exist.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ProductDto addOrUpdateProduct(ProductDto productDto, AppUser appUser) throws GenericException {
        Seller seller = sellerService.getSellerProfile(appUser).toSellerEntity();
        if (productDto.getCategoryId() == 0)
            throw new GenericException("Category id is mandatory", HttpStatus.BAD_REQUEST);
        Optional<Category> category = categoryRepository.findById(productDto.getCategoryId());
        if (!category.isPresent())
            throw new GenericException("Category is not present.", HttpStatus.NOT_FOUND);

        checkForProductExist(productDto.getName(), productDto.getBrand(), category.get(), seller);

        Product product = productDto.toProductEntity();
        List<Product> productList = seller.getProductList();
        if (Objects.isNull(productList))
            productList = new ArrayList<>();
        productList.add(product);
        seller.setProductList(productList);
        //sellerRepository.save(seller);

        List<Product> categoryProductList = category.get().getProductList();
        if (Objects.isNull(categoryProductList))
            categoryProductList = new ArrayList<>();
        categoryProductList.add(product);
        category.get().setProductList(categoryProductList);
        //categoryRepository.save(category.get());

        product.setCategory(category.get());
        product.setSeller(seller);
        product = productRepository.save(product);
        productDto = product.toProductDto();
       /* productDto.setCategoryId(product.getCategory().getId());
        productDto.setSellerId(product.getSeller().getId());*/
        productActivationEmailHandler(product);

        return productDto;
    }

    private void productActivationEmailHandler(Product product) {
        Map<String, String> map = new HashMap<>();
        map.put("Seller", product.getSeller().getFirstName());
        map.put("Product Id", String.valueOf(product.getId()));
        map.put("Product Name", product.getName());
        map.put("Product Description", product.getDescription());
        map.put("Brand Name", product.getBrand());

        String body = "<html>\n" +
                "    <body>Dear Admin,<br><br>" + product.getSeller().getFirstName() + " seller has added a " +
                "new product, Please verify the details below and activate this product: " +
                "<br><br>" + new Gson().toJson(map) + "</body>\n" +
                "</html>";
        String subject = "New Product is added by " + product.getSeller().getFirstName() + "Seller";
        emailService.sendEmail(adminEmail, subject, body);
    }

    @Override
    public List<ProductDto> getAllProducts(AppUser appUser) throws GenericException {
        SellerDto sellerDto = sellerService.getSellerProfile(appUser);
        Optional<List<Product>> productList = productRepository.findBySeller(sellerDto.toSellerEntity());
        if (productList.isPresent() && !productList.get().isEmpty())
            return productList.get().stream().map(Product::toProductDto)
                    .collect(Collectors.toList());
        throw new GenericException("No content found", HttpStatus.NOT_FOUND);
    }

    @Override
    public String activateProduct(long id) throws GenericException {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            productOptional.get().setActive(true);
            productRepository.save(productOptional.get());
            productActivationConfirmationEmailHandler(productOptional.get());
            return ApplicationConstants.SUCCESS_RESPONSE;
        }
        throw new GenericException("No Product found", HttpStatus.NOT_FOUND);
    }

    @Override
    public String deleteProduct(AppUser principal, long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().getSeller().getEmail().equals(principal.getUsername()) &&
                !product.get().isDeleted()) {
            product.get().setDeleted(true);
            productRepository.save(product.get());
        }
        return ApplicationConstants.SUCCESS_RESPONSE;
    }

    @Override
    public ProductDto getProductById(AppUser principal, long id) throws GenericException {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().getSeller().getEmail().equals(principal.getUsername()) &&
                !product.get().isDeleted()) {
            return product.get().toProductDto();
        }
        throw new GenericException("No product found for given id and seller", HttpStatus.NOT_FOUND);
    }

    @Override
    public Product getProductById(long id) throws GenericException {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent())
            return product.get();
        throw new GenericException("No product found", HttpStatus.NOT_FOUND);
    }

    private void productActivationConfirmationEmailHandler(Product product) {
        String body = "<html>\n" +
                "<body>Dear " + product.getSeller().getFirstName() + "<br><br> Your " + product.getName() +
                " product has been activated by admin.</body></html>";
        String subject = "Product " + product.getName() + " is activated!";
        emailService.sendEmail(product.getSeller().getEmail(), subject, body);
    }
}
