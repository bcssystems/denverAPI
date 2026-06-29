package com.bcsystems.intranet.util;

import com.bcsystems.intranet.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodigoGeneratorService {

    private final ProductoRepository productoRepository;

    public CodigoGeneratorService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public String generarSku() {
        long count = productoRepository.count();
        String prefix = "SOM";
        String number = String.format("%05d", count + 1);
        String sku = prefix + number;

        while (productoRepository.findBySkuIgnoreCase(sku).isPresent()) {
            count++;
            number = String.format("%05d", count);
            sku = prefix + number;
        }

        return sku;
    }

    public String generarSkuVariante(String parentSku, List<String> codigosValores) {
        String base = parentSku.replace("-", "");
        String suffix = String.join("", codigosValores);
        String sku = base + suffix;

        int counter = 0;
        while (productoRepository.findBySkuIgnoreCase(sku).isPresent()) {
            counter++;
            sku = base + suffix + counter;
        }

        return sku;
    }
}
