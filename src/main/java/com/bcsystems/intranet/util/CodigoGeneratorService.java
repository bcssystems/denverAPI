package com.bcsystems.intranet.util;

import com.bcsystems.intranet.repository.ProductoRepository;
import org.springframework.stereotype.Service;

@Service
public class CodigoGeneratorService {

    private final ProductoRepository productoRepository;

    public CodigoGeneratorService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public String generarSku() {
        long count = productoRepository.count();
        String prefix = "SOM";
        String number = String.format("%06d", count + 1);
        String sku = prefix + "-" + number;

        while (productoRepository.findBySkuIgnoreCase(sku).isPresent()) {
            count++;
            number = String.format("%06d", count);
            sku = prefix + "-" + number;
        }

        return sku;
    }
}
