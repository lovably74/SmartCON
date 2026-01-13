import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { defineConfig, loadEnv } from "vite";
import { visualizer } from "rollup-plugin-visualizer";

export default defineConfig(({ mode }) => {
  // 환경 변수 로드
  const env = loadEnv(mode, process.cwd(), '');
  const isProduction = mode === 'production';
  const isAnalyze = mode === 'analyze';
  
  const plugins = [
    react(),
    tailwindcss(),
  ];
  
  // 번들 분석 모드일 때 visualizer 플러그인 추가
  if (isAnalyze) {
    plugins.push(
      visualizer({
        filename: 'dist/stats.html',
        open: true,
        gzipSize: true,
        brotliSize: true,
        template: 'treemap', // 'sunburst', 'treemap', 'network'
      })
    );
  }

  return {
    plugins,
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    server: {
      port: 5173,
      strictPort: false,
      host: true,
    },
    build: {
      // 프로덕션 최적화 설정
      target: ["es2022", "chrome89", "firefox89", "safari15"],
      minify: isProduction ? "esbuild" : false,
      sourcemap: !isProduction,
      cssCodeSplit: true,
      
      // ESBuild 최적화 옵션
      esbuild: isProduction ? {
        drop: ['console', 'debugger'], // 프로덕션에서 console.log 제거
        legalComments: 'none', // 라이센스 주석 제거
        minifyIdentifiers: true,
        minifySyntax: true,
        minifyWhitespace: true,
      } : false,
      
      // 번들 크기 최적화
      rollupOptions: {
        // 외부 의존성 최적화
        external: [],
        
        output: {
          // 고급 코드 스플리팅 설정
          manualChunks: (id) => {
            // Node modules 처리
            if (id.includes('node_modules')) {
              // React 관련 라이브러리
              if (id.includes('react') || id.includes('react-dom')) {
                return 'react-vendor';
              }
              
              // Radix UI 컴포넌트들
              if (id.includes('@radix-ui')) {
                return 'radix-ui';
              }
              
              // 상태 관리 라이브러리
              if (id.includes('zustand') || id.includes('@tanstack/react-query')) {
                return 'state-management';
              }
              
              // 유틸리티 라이브러리
              if (id.includes('clsx') || id.includes('tailwind-merge') || 
                  id.includes('class-variance-authority') || id.includes('date-fns')) {
                return 'utilities';
              }
              
              // 아이콘 라이브러리
              if (id.includes('lucide-react')) {
                return 'icons';
              }
              
              // 라우팅 라이브러리
              if (id.includes('wouter')) {
                return 'routing';
              }
              
              // 알림 라이브러리
              if (id.includes('sonner')) {
                return 'notifications';
              }
              
              // 기타 vendor 라이브러리
              return 'vendor';
            }
            
            // 애플리케이션 코드 스플리팅
            if (id.includes('/src/pages/')) {
              // 페이지별 청크 분리
              const pagePath = id.split('/src/pages/')[1];
              const pageDir = pagePath.split('/')[0];
              return `page-${pageDir}`;
            }
            
            if (id.includes('/src/components/ui/')) {
              return 'ui-components';
            }
            
            if (id.includes('/src/components/')) {
              return 'components';
            }
            
            if (id.includes('/src/stores/')) {
              return 'stores';
            }
            
            if (id.includes('/src/hooks/')) {
              return 'hooks';
            }
            
            if (id.includes('/src/lib/') || id.includes('/src/utils/')) {
              return 'lib-utils';
            }
            
            // 기본값 반환
            return undefined;
          },
          
          // 청크 파일명 최적화
          chunkFileNames: (chunkInfo) => {
            const name = chunkInfo.name || 'chunk';
            return `js/${name}-[hash:8].js`;
          },
          
          // 에셋 파일명 최적화
          assetFileNames: (assetInfo) => {
            const info = assetInfo.name?.split(".") || [];
            const ext = info[info.length - 1];
            
            if (/png|jpe?g|svg|gif|tiff|bmp|ico|webp/i.test(ext || "")) {
              return `images/[name]-[hash:8][extname]`;
            }
            
            if (/css/i.test(ext || "")) {
              return `css/[name]-[hash:8][extname]`;
            }
            
            if (/woff2?|eot|ttf|otf/i.test(ext || "")) {
              return `fonts/[name]-[hash:8][extname]`;
            }
            
            return `assets/[name]-[hash:8][extname]`;
          },
          
          // 엔트리 파일명 최적화
          entryFileNames: "js/[name]-[hash:8].js",
          
          // 압축 최적화
          compact: isProduction,
          
          // 모듈 포맷 최적화
          format: 'es',
          
          // 청크 로딩 최적화
          inlineDynamicImports: false,
        },
        
        // Tree-shaking 최적화
        treeshake: isProduction ? {
          moduleSideEffects: false,
          propertyReadSideEffects: false,
          unknownGlobalSideEffects: false,
        } : false,
      },
      
      // 청크 크기 최적화
      chunkSizeWarningLimit: 800, // 800KB로 더 엄격하게 설정
      
      // 압축 및 보고 설정
      reportCompressedSize: isProduction,
      
      // 빌드 출력 설정
      outDir: "dist",
      emptyOutDir: true,
      
      // 에셋 인라인 임계값 (4KB 미만은 base64로 인라인)
      assetsInlineLimit: 4096,
      
      // CSS 최적화
      cssMinify: isProduction ? 'esbuild' : false,
      
      // 모듈 사전 로드 최적화
      modulePreload: {
        polyfill: true,
      },
    },
    
    // 의존성 최적화 설정
    optimizeDeps: {
      include: [
        "react",
        "react-dom",
        "react/jsx-runtime",
        "zustand",
        "@tanstack/react-query",
        "wouter",
        "clsx",
        "tailwind-merge",
        "date-fns",
        "lucide-react",
        "sonner"
      ],
      exclude: [
        // 큰 라이브러리는 동적 임포트로 처리
      ],
      // 의존성 스캔 최적화
      entries: [
        'src/main.tsx',
        'src/App.tsx'
      ],
    },
    
    // 환경 변수 및 상수 정의
    define: {
      __APP_VERSION__: JSON.stringify(env['VITE_APP_VERSION'] || "1.0.0"),
      __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
      __DEV__: JSON.stringify(!isProduction),
      __PROD__: JSON.stringify(isProduction),
    },
    
    // 워커 설정
    worker: {
      format: 'es',
      plugins: () => [react()],
    },
    
    // JSON 최적화
    json: {
      namedExports: true,
      stringify: false,
    },
  };
});

