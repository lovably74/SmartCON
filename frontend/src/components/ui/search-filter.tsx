import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Search, X, Filter } from "lucide-react";

/**
 * SearchFilter 공통 컴포넌트
 * Requirements: 3.4, 3.5
 * 
 * 기능:
 * - 통합 검색
 * - 다중 필터 지원
 * - 필터 태그 표시
 * - 모바일 반응형
 */

export interface FilterOption {
  key: string;
  label: string;
  options: { value: string; label: string }[];
}

export interface SearchFilterProps {
  searchPlaceholder?: string;
  filters?: FilterOption[];
  onSearch?: (query: string) => void;
  onFilterChange?: (filters: Record<string, string>) => void;
  showFilterCount?: boolean;
}

export function SearchFilter({
  searchPlaceholder = "검색...",
  filters = [],
  onSearch,
  onFilterChange,
  showFilterCount = true,
}: SearchFilterProps) {
  const [searchQuery, setSearchQuery] = useState("");
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [showFilters, setShowFilters] = useState(false);

  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    onSearch?.(value);
  };

  const handleFilterChange = (key: string, value: string) => {
    const newFilters = { ...activeFilters };
    
    if (value === "all" || !value) {
      delete newFilters[key];
    } else {
      newFilters[key] = value;
    }
    
    setActiveFilters(newFilters);
    onFilterChange?.(newFilters);
  };

  const handleClearFilter = (key: string) => {
    const newFilters = { ...activeFilters };
    delete newFilters[key];
    setActiveFilters(newFilters);
    onFilterChange?.(newFilters);
  };

  const handleClearAll = () => {
    setSearchQuery("");
    setActiveFilters({});
    onSearch?.("");
    onFilterChange?.({});
  };

  const activeFilterCount = Object.keys(activeFilters).length;
  const hasActiveFilters = activeFilterCount > 0 || searchQuery.length > 0;

  return (
    <div className="space-y-3">
      {/* 검색 바 및 필터 토글 */}
      <div className="flex flex-col sm:flex-row gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder={searchPlaceholder}
            value={searchQuery}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="pl-10 pr-10"
          />
          {searchQuery && (
            <Button
              variant="ghost"
              size="icon"
              className="absolute right-1 top-1/2 transform -translate-y-1/2 h-7 w-7"
              onClick={() => handleSearchChange("")}
            >
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
        
        {filters.length > 0 && (
          <Button
            variant="outline"
            onClick={() => setShowFilters(!showFilters)}
            className="gap-2"
          >
            <Filter className="h-4 w-4" />
            필터
            {showFilterCount && activeFilterCount > 0 && (
              <Badge variant="secondary" className="ml-1 px-1.5 py-0 text-xs">
                {activeFilterCount}
              </Badge>
            )}
          </Button>
        )}
        
        {hasActiveFilters && (
          <Button variant="ghost" onClick={handleClearAll} className="gap-2">
            <X className="h-4 w-4" />
            초기화
          </Button>
        )}
      </div>

      {/* 필터 옵션 */}
      {showFilters && filters.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 p-4 bg-muted/50 rounded-lg">
          {filters.map((filter) => (
            <div key={filter.key} className="space-y-2">
              <label className="text-sm font-medium">{filter.label}</label>
              <Select
                value={activeFilters[filter.key] || "all"}
                onValueChange={(value) => handleFilterChange(filter.key, value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="전체" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">전체</SelectItem>
                  {filter.options.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          ))}
        </div>
      )}

      {/* 활성 필터 태그 */}
      {activeFilterCount > 0 && (
        <div className="flex flex-wrap gap-2">
          {Object.entries(activeFilters).map(([key, value]) => {
            const filter = filters.find((f) => f.key === key);
            const option = filter?.options.find((o) => o.value === value);
            
            return (
              <Badge key={key} variant="secondary" className="gap-1 pr-1">
                <span className="text-xs">
                  {filter?.label}: {option?.label}
                </span>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-4 w-4 p-0 hover:bg-transparent"
                  onClick={() => handleClearFilter(key)}
                >
                  <X className="h-3 w-3" />
                </Button>
              </Badge>
            );
          })}
        </div>
      )}
    </div>
  );
}
