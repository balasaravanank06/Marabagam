package com.marabagam.art.service;

import com.marabagam.art.domain.Category;
import com.marabagam.art.domain.District;
import com.marabagam.art.dto.CategoryDto;
import com.marabagam.art.dto.DistrictDto;
import com.marabagam.art.repository.CategoryRepository;
import com.marabagam.art.repository.DistrictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryDistrictService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DistrictRepository districtRepository;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToDto)
                .toList();
    }

    public List<DistrictDto> getAllDistricts() {
        return districtRepository.findAll().stream()
                .map(this::mapDistrictToDto)
                .toList();
    }

    public CategoryDto mapCategoryToDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getNameTa(), c.getDescription(), c.getDescriptionTa());
    }

    public DistrictDto mapDistrictToDto(District d) {
        return new DistrictDto(
            d.getId(), d.getName(), d.getNameTa(), d.getCode(),
            d.getLatitude(), d.getLongitude(),
            d.getShortHistory(), d.getShortHistoryTa(),
            d.getCulturalSignificance(), d.getCulturalSignificanceTa(),
            d.getGeojsonFeatureCode(), 0, 0, 0
        );
    }
}
